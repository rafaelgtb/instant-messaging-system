import * as React from "react";
import { ReactNode } from "react";

const Modal = ({
  title,
  children,
  onClose,
}: {
  title: string;
  children: ReactNode;
  onClose: () => void;
}) => {
  return (
    <div
      className="modal-overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="modal-box">
        <h2 id="modal-title">{title}</h2>
        {children}
        <button
          onClick={onClose}
          className="modal-close-button"
          aria-label="Close Modal"
        >
          &times;
        </button>
      </div>
    </div>
  );
};

export default Modal;
