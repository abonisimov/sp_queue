package net.alex.game.queue.controller;

@FunctionalInterface
public interface MVCExecutable {
    void execute(String token) throws Exception;
}
