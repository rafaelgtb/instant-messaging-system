import * as React from "react";
import * as ReactDOM from "react-dom/client";
import App from "./App";

const container = document.getElementById("container");
if (container) {
  const root = ReactDOM.createRoot(container);
  root.render(<App />);
} else {
  console.error("Container element not found");
}
