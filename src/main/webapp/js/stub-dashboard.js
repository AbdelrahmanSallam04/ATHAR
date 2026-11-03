document.addEventListener("DOMContentLoaded", async () => {
  const userNameElem = document.getElementById("userName");
  const userRoleElem = document.getElementById("userRole");
  const userAvatarElem = document.getElementById("userAvatar");
  const logoutBtn = document.getElementById("logoutBtn");

  // Fetch current authenticated user from API
  try {
    const res = await fetch("/api/me");
    const data = await res.json();

    if (res.ok && data.authenticated) {
      if (userNameElem) userNameElem.textContent = data.name;
      if (userRoleElem) userRoleElem.textContent = formatRole(data.role);
      if (userAvatarElem) userAvatarElem.textContent = getInitials(data.name);
    } else {
      // Fallback to local storage if available, else redirect to login
      const cached = sessionStorage.getItem("athar_user");
      if (cached) {
        const u = JSON.parse(cached);
        if (userNameElem) userNameElem.textContent = u.name;
        if (userRoleElem) userRoleElem.textContent = formatRole(u.role);
        if (userAvatarElem) userAvatarElem.textContent = getInitials(u.name);
      } else {
        window.location.href = "/login.html";
      }
    }
  } catch (err) {
    console.warn("Could not check authentication status:", err);
  }

  // Handle Logout
  if (logoutBtn) {
    logoutBtn.addEventListener("click", async (e) => {
      e.preventDefault();
      try {
        await fetch("/api/logout", { method: "POST" });
      } catch (e) {}
      sessionStorage.clear();
      window.location.href = "/login.html";
    });
  }

  function formatRole(role) {
    if (!role) return "Member";
    return role.split("_")
      .map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
      .join(" ");
  }

  function getInitials(name) {
    if (!name) return "U";
    return name.split(" ")
      .map(n => n.charAt(0).toUpperCase())
      .slice(0, 2)
      .join("");
  }
});
