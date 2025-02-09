import { LoginInput, RegisterInput } from "../types";

export type FormState<Input> =
  | { tag: "editing"; error?: string; inputs: Input }
  | { tag: "submitting"; inputs: Input }
  | { tag: "redirect" };

export type FormAction =
  | { type: "edit"; inputName: keyof any; inputValue: string }
  | { type: "submit" }
  | { type: "success" }
  | { type: "error"; message: string };

export const formReducers = <Input>(
  state: FormState<Input>,
  action: FormAction,
): FormState<Input> => {
  switch (state.tag) {
    case "editing":
      switch (action.type) {
        case "edit":
          return {
            ...state,
            inputs: { ...state.inputs, [action.inputName]: action.inputValue },
            error: undefined,
          };
        case "submit":
          return { tag: "submitting", inputs: state.inputs };
        default:
          return state;
      }

    case "submitting":
      switch (action.type) {
        case "success":
          return { tag: "redirect" };
        case "error":
          return {
            tag: "editing",
            error: action.message,
            inputs: state.inputs,
          };
        default:
          return state;
      }
    case "redirect":
      return state;
    default:
      return state;
  }
};

export const loginReducer = (
  state: FormState<LoginInput>,
  action: FormAction,
): FormState<LoginInput> => formReducers(state, action);

export const registerReducer = (
  state: FormState<RegisterInput>,
  action: FormAction,
): FormState<RegisterInput> => formReducers(state, action);
