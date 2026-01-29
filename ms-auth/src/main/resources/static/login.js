(() => {
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");
  const submitBtn = document.getElementById("submit-btn");
  const usernameHelp = document.getElementById("username-help");
  const passwordHelp = document.getElementById("password-help");
  const formError = document.getElementById("form-error");

  const commonPasswords = new Set([
    "123456",
    "123456789",
    "qwerty",
    "password",
    "111111",
    "12345678",
    "abc123",
    "1234567",
    "password1",
    "12345",
    "123123",
    "admin",
    "iloveyou",
  ]);

  function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
  }

  function showServerError() {
    const error = getQueryParam("error");
    if (!error) {
      formError.classList.remove("show");
      formError.textContent = "";
      return;
    }
    if (error === "validation") {
      formError.textContent = "Datos invalidos. Revisa cedula y contrasena.";
    } else {
      formError.textContent = "Credenciales invalidas.";
    }
    formError.classList.add("show");
  }

  function validateUsername() {
    const value = (usernameInput.value || "").trim();
    if (!value) {
      usernameHelp.textContent = "";
      usernameHelp.classList.remove("error");
      usernameInput.classList.remove("invalid");
      return false;
    }
    const valid = /^\d{10}$/.test(value);
    if (!valid) {
      usernameHelp.textContent = "La cedula debe tener 10 digitos numericos.";
      usernameHelp.classList.add("error");
      usernameInput.classList.add("invalid");
      return false;
    }
    usernameHelp.textContent = "Cedula valida.";
    usernameHelp.classList.remove("error");
    usernameInput.classList.remove("invalid");
    return true;
  }

  function validatePassword() {
    const value = passwordInput.value || "";
    if (!value) {
      passwordHelp.textContent = "";
      passwordHelp.classList.remove("error");
      passwordInput.classList.remove("invalid");
      return false;
    }
    if (value.length < 8) {
      return setPasswordError("Minimo 8 caracteres.");
    }
    if (!/[a-z]/.test(value)) {
      return setPasswordError("Incluye al menos una minuscula.");
    }
    if (!/[A-Z]/.test(value)) {
      return setPasswordError("Incluye al menos una mayuscula.");
    }
    if (!/\d/.test(value)) {
      return setPasswordError("Incluye al menos un numero.");
    }
    if (!/[^\\w\\s]/.test(value)) {
      return setPasswordError("Incluye al menos un simbolo.");
    }
    if (commonPasswords.has(value.toLowerCase())) {
      return setPasswordError("Contrasena demasiado comun.");
    }
    passwordHelp.textContent = "Contrasena valida.";
    passwordHelp.classList.remove("error");
    passwordInput.classList.remove("invalid");
    return true;
  }

  function setPasswordError(message) {
    passwordHelp.textContent = message;
    passwordHelp.classList.add("error");
    passwordInput.classList.add("invalid");
    return false;
  }

  function updateButton() {
    const validUser = validateUsername();
    const validPass = validatePassword();
    submitBtn.disabled = !(validUser && validPass);
  }

  usernameInput.addEventListener("input", updateButton);
  passwordInput.addEventListener("input", updateButton);

  showServerError();
  updateButton();
})();
