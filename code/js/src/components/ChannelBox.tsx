import * as React from "react";
import { useNavigate } from "react-router-dom";
import { Channel } from "../types";
import "./CSS/ChannelBox.css";
import { useJoinPublicChannel } from "../hooks/channels/useJoinChannel";

interface ChannelBoxProps {
  channel: Channel;
  isJoined: boolean;
}

const ChannelBox = ({ channel, isJoined }: ChannelBoxProps) => {
  const { handleJoin, loading, error } = useJoinPublicChannel(channel.id);
  const navigate = useNavigate();

  const handleClick = async () => {
    if (!isJoined) await handleJoin(channel.id);
    navigate(`/channels/${channel.id}`);
  };

  return (
    <div
      onClick={handleClick}
      className="channel-box"
      style={{ cursor: "pointer" }}
    >
      <h3 className="channel-box-title">{channel.name}</h3>
      {loading && <span>Joining...</span>}
      {error && <div style={{ color: "red", marginTop: "8px" }}>{error}</div>}
    </div>
  );
};

export default ChannelBox;
