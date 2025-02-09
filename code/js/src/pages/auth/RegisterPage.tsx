import * as React from "react";
import { fetchCurrentUser } from "../../api/users";
import { setCookie } from "../../utils/cookies";
import { registerReducer } from "../../reducers/formReducers";
import "../CSS/authenticationPages.css";
import useAuth from "../../hooks/auth/useAuth";
import { loginUser, registerUser } from "../../api/auth";
import { RegisterInput } from "../../types";
import AuthForm from "../../components/AuthForm";

const RegisterPage = () => {
  const { setUser } = useAuth();

  const handleRegister = async (inputs: RegisterInput) => {
    const { username, password, invitationToken } = inputs;
    await registerUser({ username, password, invitationToken });
    const token = await loginUser({ username, password });
    setCookie("token", token.tokenValue);
    const currentUser = await fetchCurrentUser();
    setUser(currentUser);
  };

  return (
    <AuthForm<RegisterInput>
      initialInputs={{ username: "", password: "", invitationToken: "" }}
      reducer={registerReducer}
      onSubmit={handleRegister}
      title="Register"
      submitButtonText="Register"
      submittingButtonText="Registering..."
    />
  );
};

export default RegisterPage;
