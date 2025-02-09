export const setCookie = (name: string, value: string, days: number = 7) => {
  document.cookie =
    `${name}=${value || ""};` +
    `expires=${new Date(Date.now() + days * 864e5).toUTCString()};` +
    `path=/;` +
    `SameSite=Strict`;
};

export const getCookie = (name: string): string | undefined =>
  document.cookie
    .split("; ")
    .find((cookie) => cookie.startsWith(`${name}=`))
    ?.substring(`${name}=`.length);

export const deleteCookie = (name: string) => setCookie(name, "", -1);
