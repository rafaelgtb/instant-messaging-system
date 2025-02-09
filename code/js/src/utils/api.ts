export const handleResponse = async <T>(res: Response): Promise<T> => {
  const contentType = res.headers.get("Content-Type") || "";
  let data: any;

  if (res.status === 204 || res.headers.get("content-length") === "0") {
    return {} as T;
  }

  try {
    data =
      contentType.includes("application/json") ||
      contentType.includes("application/problem+json")
        ? await res.json()
        : await res.text();
  } catch (error) {
    throw new Error("Invalid response from server");
  }

  if (!res.ok) {
    throw new Error(data?.detail || "An error occurred");
  }

  return data as T;
};

export const apiRequest = async <T>(
  endpoint: string,
  options: RequestInit = {},
): Promise<T> => {
  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  const response = await fetch(endpoint, {
    ...options,
    headers,
    credentials: "include",
  });

  return handleResponse<T>(response);
};
