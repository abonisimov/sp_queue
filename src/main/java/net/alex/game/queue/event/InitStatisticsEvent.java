package net.alex.game.queue.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.alex.game.model.event.GameEvent;

@Getter
@SuperBuilder
public class InitStatisticsEvent extends GameEvent implements SystemEvent {}
