package com.zaca.stillstanding.domain.dto.event;

import com.zaca.stillstanding.domain.dto.AnswerType;
import com.zaca.stillstanding.domain.dto.MatchEvent;

import lombok.Data;

/**
 * @author hundun
 * Created on 2021/05/10
 */
@Data
public class SkillResultEvent extends MatchEvent {
    String teamName;
    String roleName;
    String skillName;
    int skillRemainTime;
    String staticData;
}
