import * as React from "react";
import { fetchCurrentUser } from "../../api/users";
import { setCookie } from "../../utils/cookies";
import { loginReducer } from "../../reducers/formReducers";
import "../CSS/authenticationPages.css";
import useAuth from "../../hooks/auth/useAuth";
import { loginUser } from "../../api/auth";
import AuthForm from "../../components/AuthForm";
import {LoginInput} from "../../types";

const LoginPage = () => {
  const { setUser } = useAuth();

  const handleLogin = async (inputs: LoginInput) => {
    const { username, password } = inputs;
    const token = await loginUser({ username, password });
    setCookie("token", token.tokenValue);
    const user = await fetchCurrentUser();
    setUser(user);
  };

  return (
    <AuthForm<LoginInput>
      initialInputs={{ username: "", password: "" }}
      reducer={loginReducer}
      onSubmit={handleLogin}
      title="Login"
      submitButtonText="Login"
      submittingButtonText="Logging in..."
    />
  );
};

export default LoginPage;
