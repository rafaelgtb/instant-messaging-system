import * as React from "react";
import { ReactNode } from "react";
import useLogout from "../hooks/auth/useLogout";

interface LogoutButtonProps {
  children: ReactNode;
  className?: string;
}

const LogoutButton = ({ children, className }: LogoutButtonProps) => {
  const { handleLogout, loading, error } = useLogout();

  return (
    <div>
      <button onClick={handleLogout} disabled={loading} className={className}>
        {loading ? "Logging out..." : children}
      </button>
      {error && <div style={{ color: "red", marginTop: "8px" }}>{error}</div>}
    </div>
  );
};

export default LogoutButton;
