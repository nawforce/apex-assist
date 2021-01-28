import { Reciever } from "./Receiver";
import { TestHandler } from "./TestHandler";

export interface Handler {
    requestDependents(name: string): void
}

export function createHandler(reciever: Reciever): Handler {
    return new TestHandler(reciever)
}