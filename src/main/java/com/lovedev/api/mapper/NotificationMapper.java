package com.lovedev.api.mapper;

import com.lovedev.api.model.dto.response.NotificationResponse;
import com.lovedev.api.model.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {

    @Mapping(target = "body", source = "body")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}