import * as React from "react";
import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import useAuth from "../hooks/auth/useAuth";

const AuthRequire = ({ children }: { children: ReactNode }) => {
  const { user, loading, error } = useAuth();
  const location = useLocation();

  if (loading) return <div></div>;
  if (error) return <div>{error.message}</div>;
  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default AuthRequire;
