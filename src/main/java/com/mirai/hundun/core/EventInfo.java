package com.mirai.hundun.core;

import javax.validation.constraints.NotNull;

import lombok.Data;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * @author hundun
 * Created on 2021/05/07
 */
@Data
public class EventInfo {
    String characterId;
    long groupId;
    long senderId;
    long targetId;
    MessageChain message;
}
