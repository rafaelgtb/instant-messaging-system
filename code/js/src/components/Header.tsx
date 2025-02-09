import * as React from "react";
import { Link } from "react-router-dom";
import { Channel } from "../types";

interface HeaderProps {
  channel: Channel;
  channelId: number;
  onLeave: () => void;
}

const Header = ({ channel, channelId, onLeave }: HeaderProps) => (
  <header className="chat-header">
    <h1>{channel.name}</h1>
    <div>
      {!channel.isPublic && (
        <Link to={`/channels/${channelId}/manage-invitations`}>
          <button
            className="manage-invitations-button"
            aria-label="Manage Invitations"
          >
            Manage Invitations
          </button>
        </Link>
      )}
      <button
        className="leave-channel-button"
        onClick={onLeave}
        aria-label="Leave Channel"
      >
        Leave Channel
      </button>
    </div>
  </header>
);

export default Header;
