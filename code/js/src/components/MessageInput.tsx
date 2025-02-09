import * as React from "react";
import { FormEvent } from "react";

interface MessageInputProps {
  newMessage: string;
  setNewMessage: React.Dispatch<React.SetStateAction<string>>;
  handleSubmit: (e: FormEvent<HTMLFormElement>) => void;
  isPosting: boolean;
  postError: string | null;
  generalError: string | null;
}

const MessageInput = ({
  newMessage,
  setNewMessage,
  handleSubmit,
  isPosting,
  postError,
  generalError,
}: MessageInputProps) => (
  <form onSubmit={handleSubmit} className="chat-input-container">
    <textarea
      id="newMessage"
      placeholder="Type a message..."
      value={newMessage}
      onChange={(e) => setNewMessage(e.target.value)}
      className="chat-input"
      rows={2}
      disabled={isPosting}
      required
      aria-label="New Message"
    ></textarea>
    <button
      type="submit"
      className="send-button"
      disabled={isPosting || !newMessage.trim()}
      aria-label="Send Message"
    >
      {isPosting ? "Sending..." : "Send"}
    </button>
    {generalError && <div className="error-message">{generalError}</div>}
    {postError && <div className="error-message">{postError}</div>}
  </form>
);

export default MessageInput;
