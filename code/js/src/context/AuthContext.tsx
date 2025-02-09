import * as React from "react";
import { createContext, ReactNode, useEffect, useState } from "react";
import { fetchCurrentUser } from "../api/users";
import { User } from "../types";

export type AuthContextType = {
  user: User | undefined;
  setUser: (user: User | undefined) => void;
  loading: boolean;
  error: Error | undefined;
};

export const AuthContext = createContext<AuthContextType>({
  user: undefined,
  setUser: () => {
    throw new Error("setUser called outside of AuthProvider");
  },
  loading: true,
  error: undefined,
});

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | undefined>(undefined);

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const user = await fetchCurrentUser();
        setUser(user || undefined);
      } catch (err) {
        setError(err as Error);
        setUser(undefined);
      } finally {
        setLoading(false);
      }
    };
    initializeAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ user, setUser, loading, error }}>
      {children}
    </AuthContext.Provider>
  );
};
