document.addEventListener("DOMContentLoaded", () => {
  const signupForm = document.getElementById("signupForm");
  const roleCards = document.querySelectorAll("#roleGrid .method-card");
  const selectedRoleInput = document.getElementById("selectedRole");
  const volunteerFields = document.getElementById("volunteerFields");
  const alertBanner = document.getElementById("signupAlert");
  const submitBtn = document.getElementById("submitBtn");

  const nameInput = document.getElementById("name");
  const contactInput = document.getElementById("contactInfo");
  const passwordInput = document.getElementById("password");
  const confirmPasswordInput = document.getElementById("confirmPassword");
  const toggleBtn = document.getElementById("togglePasswordBtn");

  // Handle Role Selection
  roleCards.forEach((card) => {
    card.addEventListener("click", () => {
      roleCards.forEach((c) => c.classList.remove("selected"));
      card.classList.add("selected");
      const role = card.getAttribute("data-role");
      selectedRoleInput.value = role;

      // Show/hide volunteer fields
      if (role === "VOLUNTEER") {
        volunteerFields.style.display = "block";
      } else {
        volunteerFields.style.display = "none";
      }
    });
  });

  // Toggle Chip Selections
  document.querySelectorAll(".chip-button").forEach((chip) => {
    chip.addEventListener("click", () => {
      chip.classList.toggle("selected");
    });
  });

  // Toggle Password Visibility
  toggleBtn.addEventListener("click", () => {
    const isPassword = passwordInput.getAttribute("type") === "password";
    passwordInput.setAttribute("type", isPassword ? "text" : "password");
    confirmPasswordInput.setAttribute("type", isPassword ? "text" : "password");
    toggleBtn.textContent = isPassword ? "🔒" : "👁️";
  });

  // Handle Form Submission
  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert();

    const name = nameInput.value.trim();
    const contactInfo = contactInput.value.trim();
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const role = selectedRoleInput.value;

    let valid = true;

    if (!name) {
      nameInput.classList.add("invalid");
      document.getElementById("nameError").style.display = "block";
      valid = false;
    } else {
      nameInput.classList.remove("invalid");
      document.getElementById("nameError").style.display = "none";
    }

    if (!contactInfo) {
      contactInput.classList.add("invalid");
      document.getElementById("contactError").style.display = "block";
      valid = false;
    } else {
      contactInput.classList.remove("invalid");
      document.getElementById("contactError").style.display = "none";
    }

    if (!password || password.length < 6) {
      passwordInput.classList.add("invalid");
      document.getElementById("passwordError").style.display = "block";
      valid = false;
    } else {
      passwordInput.classList.remove("invalid");
      document.getElementById("passwordError").style.display = "none";
    }

    if (password !== confirmPassword) {
      confirmPasswordInput.classList.add("invalid");
      document.getElementById("confirmPasswordError").style.display = "block";
      valid = false;
    } else {
      confirmPasswordInput.classList.remove("invalid");
      document.getElementById("confirmPasswordError").style.display = "none";
    }

    if (!valid) return;

    // Collect skills & preferences if volunteer
    const skills = [];
    const preferences = [];
    if (role === "VOLUNTEER") {
      document.querySelectorAll("#skillsChips .chip-button.selected").forEach((chip) => {
        skills.push(chip.getAttribute("data-value"));
      });
      document.querySelectorAll("#preferencesChips .chip-button.selected").forEach((chip) => {
        preferences.push(chip.getAttribute("data-value"));
      });
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = "Creating Account...";

    try {
      const response = await fetch("/api/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name,
          contactInfo,
          password,
          role,
          skills,
          preferences,
        }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showToast("Account created successfully! Redirecting to login...", "success");
        setTimeout(() => {
          window.location.href = "login.html";
        }, 1200);
      } else {
        showAlert(data.error || "Signup failed. Please try again.", "danger");
      }
    } catch (err) {
      console.error(err);
      showAlert("Network error. Could not connect to the ATHAR server.", "danger");
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = "<span>Register Account</span>";
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
