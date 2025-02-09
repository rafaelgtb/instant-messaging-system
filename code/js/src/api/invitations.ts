import { Invitation, InvitationInput } from "../types";
import { apiRequest } from "../utils/api";

export const createInvitation = async (
  input: InvitationInput,
): Promise<Invitation> => {
  return apiRequest<Invitation>(
    `/api/channels/${input.channelId}/invitations`,
    {
      method: "POST",
      body: JSON.stringify(input),
    },
  );
};

export const fetchInvitations = async (
  channelId: number,
): Promise<Invitation[]> => {
  return apiRequest<Invitation[]>(`/api/channels/${channelId}/invitations`, {
    method: "GET",
  });
};

export const revokeInvitations = async (
  channelId: number,
  invitationId: number,
): Promise<string> => {
  return apiRequest<string>(
    `/api/channels/${channelId}/invitations/${invitationId}/revoke`,
    {
      method: "POST",
    },
  );
};
