document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  const contactInput = document.getElementById("contactInfo");
  const passwordInput = document.getElementById("password");
  const toggleBtn = document.getElementById("togglePasswordBtn");
  const alertBanner = document.getElementById("loginAlert");
  const submitBtn = document.getElementById("submitBtn");

  // Toggle Password Visibility
  toggleBtn.addEventListener("click", () => {
    const isPassword = passwordInput.getAttribute("type") === "password";
    passwordInput.setAttribute("type", isPassword ? "text" : "password");
    toggleBtn.textContent = isPassword ? "🔒" : "👁️";
  });

  // Handle Form Submission
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert();

    const contactInfo = contactInput.value.trim();
    const password = passwordInput.value;

    let valid = true;
    if (!contactInfo) {
      contactInput.classList.add("invalid");
      document.getElementById("contactError").style.display = "block";
      valid = false;
    } else {
      contactInput.classList.remove("invalid");
      document.getElementById("contactError").style.display = "none";
    }

    if (!password) {
      passwordInput.classList.add("invalid");
      document.getElementById("passwordError").style.display = "block";
      valid = false;
    } else {
      passwordInput.classList.remove("invalid");
      document.getElementById("passwordError").style.display = "none";
    }

    if (!valid) return;

    // Disable submit button while sending
    submitBtn.disabled = true;
    submitBtn.innerHTML = "Signing in...";

    try {
      const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ contactInfo, password }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        // Store session data in sessionStorage
        sessionStorage.setItem("athar_token", data.token);
        sessionStorage.setItem("athar_user", JSON.stringify({
          userId: data.userId,
          name: data.name,
          contactInfo: data.contactInfo,
          role: data.role
        }));

        showToast("Signed in successfully! Redirecting...", "success");

        setTimeout(() => {
          window.location.href = data.redirectUrl || "/stubs/" + data.role.toLowerCase().replace("_", "-") + "-dashboard.html";
        }, 600);
      } else {
        showAlert(data.error || "Invalid contact info or password.", "danger");
      }
    } catch (err) {
      console.error(err);
      showAlert("Network error. Please make sure the ATHAR server is active.", "danger");
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = "<span>Sign In to Portal</span>";
    }
  });

  function showAlert(msg, type) {
    alertBanner.textContent = msg;
    alertBanner.className = `alert-banner ${type}`;
    alertBanner.style.display = "block";
  }

  function hideAlert() {
    alertBanner.style.display = "none";
  }

  function showToast(msg, type) {
    const toast = document.getElementById("toast");
    const toastMsg = document.getElementById("toastMsg");
    toastMsg.textContent = msg;
    toast.className = `toast toast-${type} show`;
    setTimeout(() => {
      toast.classList.remove("show");
    }, 3000);
  }
});
