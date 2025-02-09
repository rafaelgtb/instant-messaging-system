import * as React from "react";
import { Message, User } from "../types";

interface MessageComponentProps {
  message: Message;
  currentUser: User;
}

const MessageComponent = ({ message, currentUser }: MessageComponentProps) => (
  <div
    className={`message ${
      message.user.id === currentUser.id ? "sent" : "received"
    }`}
  >
    <div className="message-header">
      {message.user?.username ? (
        <>
          <img
            src={`https://ui-avatars.com/api/?name=${encodeURIComponent(
              message.user.username,
            )}`}
            alt={`${message.user.username}'s avatar`}
            className="avatar"
          />
          <span className="message-username">{message.user.username}</span>
        </>
      ) : (
        <span className="message-username">Unknown User</span>
      )}
    </div>
    <div className="message-content">{message.content}</div>
    <div className="message-time">
      {new Date(message.createdAt).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      })}
    </div>
  </div>
);

export default MessageComponent;
