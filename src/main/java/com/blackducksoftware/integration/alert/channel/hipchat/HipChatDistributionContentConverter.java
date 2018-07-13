package com.blackducksoftware.integration.alert.channel.hipchat;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class HipChatDistributionContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public HipChatDistributionContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public Optional<? extends ConfigRestModel> getRestModelFromJson(final String json) {
        return contentConverter.getContent(json, HipChatDistributionRestModel.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final HipChatDistributionRestModel hipChatRestModel = (HipChatDistributionRestModel) restModel;
        final int roomId = Integer.parseInt(hipChatRestModel.getRoomId());
        final HipChatDistributionConfigEntity hipChatEntity = new HipChatDistributionConfigEntity(roomId, hipChatRestModel.getNotify(), hipChatRestModel.getColor());
        return hipChatEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
        final HipChatDistributionRestModel hipChatRestModel = new HipChatDistributionRestModel();
        hipChatRestModel.setRoomId(hipChatEntity.getRoomId().toString());
        hipChatRestModel.setNotify(hipChatEntity.getNotify());
        hipChatRestModel.setColor(hipChatEntity.getColor());
        return hipChatRestModel;
    }

}
