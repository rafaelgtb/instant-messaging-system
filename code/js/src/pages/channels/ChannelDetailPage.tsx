import * as React from "react";
import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import LoadingSpinner from "../../components/LoadingSpinner";
import { useParams } from "react-router-dom";
import "../CSS/ChannelDetailPage.css";
import useFetchChannel from "../../hooks/channels/useFetchChannel";
import useFetchAccessType from "../../hooks/channels/useFetchAccessType";
import useFetchMessages from "../../hooks/messages/useFetchMessages";
import useAuth from "../../hooks/auth/useAuth";
import usePostMessage from "../../hooks/messages/usePostMessage";
import useLeaveChannel from "../../hooks/channels/useLeaveChannel";
import { Message } from "../../types";
import useMessageSSE from "../../hooks/messages/useMessageSSE";
import Header from "../../components/Header";
import MessageComponent from "../../components/MessageComponent";
import MessageInput from "../../components/MessageInput";
import Modal from "../../components/Modal";

const ChannelDetailPage = () => {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const channelId = id ? parseInt(id, 10) : null;

  const {
    channel,
    loading: isChannelLoading,
    error: channelError,
  } = useFetchChannel(channelId);

  const {
    accessType,
    loading: isAccessTypeLoading,
    error: accessTypeError,
  } = useFetchAccessType(channelId);

  const {
    messages,
    setMessages,
    loading: isMessagesLoading,
    error: messagesError,
  } = useFetchMessages(channelId);

  const {
    handlePostMessage: postNewMessage,
    loading: isPosting,
    error: postError,
  } = usePostMessage(channelId);

  const {
    handleLeaveChannel: leaveChannel,
    loading: isLeaving,
    error: leaveError,
  } = useLeaveChannel(channelId);

  const [newMessage, setNewMessage] = useState<string>("");
  const [showModal, setShowModal] = useState<boolean>(false);
  const [generalError, setGeneralError] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  const handleNewMessage = useCallback(
    (message: Message) => {
      setMessages((prevMessages) => {
        if (prevMessages.some((m) => m.id === message.id)) {
          return prevMessages;
        }
        return [...prevMessages, message];
      });
      scrollToBottom();
    },
    [setMessages, scrollToBottom],
  );

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  useMessageSSE({ channelId: channelId!, onNewMessage: handleNewMessage });

  const handlePostMessageSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    await postNewMessage(newMessage);
    setNewMessage("");
  };

  const handleLeaveChannel = async () => await leaveChannel();

  if (channelId === null) {
    return <div className="error-message">Invalid channel ID.</div>;
  }

  if (!user) {
    return (
      <div className="error-message">
        You must be logged in to view this page.
      </div>
    );
  }

  if (isChannelLoading || isAccessTypeLoading || isMessagesLoading) {
    return <LoadingSpinner />;
  }

  if (channelError || accessTypeError || messagesError) {
    return (
      <div className="error-container">
        {channelError && <p>{channelError}</p>}
        {accessTypeError && <p>{accessTypeError}</p>}
        {messagesError && <p>{messagesError}</p>}
      </div>
    );
  }

  if (!channel) {
    return <div className="no-channel">No channel found.</div>;
  }

  return (
    <div className="chat-container">
      <Header
        channel={channel}
        channelId={channelId}
        onLeave={() => setShowModal(true)}
      />

      <div className="chat-messages">
        {isMessagesLoading ? (
          <LoadingSpinner />
        ) : messages.length === 0 ? (
          <div className="no-messages">
            No messages yet. Start the conversation!
          </div>
        ) : (
          messages.map((message) => (
            <MessageComponent
              key={message.id}
              message={message}
              currentUser={user}
            />
          ))
        )}
        <div ref={messagesEndRef} />
        {messagesError && <div className="error-message">{messagesError}</div>}
      </div>

      {accessType && accessType !== "READ_ONLY" && (
        <MessageInput
          newMessage={newMessage}
          setNewMessage={setNewMessage}
          handleSubmit={handlePostMessageSubmit}
          isPosting={isPosting}
          postError={postError}
          generalError={generalError}
        />
      )}

      {showModal && (
        <Modal title="Confirm Leave" onClose={() => setShowModal(false)}>
          <p>Are you sure you want to leave the channel "{channel.name}"?</p>
          {leaveError && <p className="modal-error">{leaveError}</p>}
          <div className="modal-buttons">
            <button
              className="modal-confirm-button"
              onClick={handleLeaveChannel}
              disabled={isLeaving}
              aria-label="Confirm Leave Channel"
            >
              {isLeaving ? "Leaving..." : "Confirm"}
            </button>
            <button
              className="modal-cancel-button"
              onClick={() => setShowModal(false)}
              aria-label="Cancel Leave Channel"
            >
              Cancel
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default ChannelDetailPage;
