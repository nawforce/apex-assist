import { Reciever } from "./Receiver";
import { TestHandler } from "./TestHandler";

export interface Handler {
    requestDependents(identifier: string, depth: number): void
}

export function createHandler(reciever: Reciever): Handler {
    return new TestHandler(reciever)
}