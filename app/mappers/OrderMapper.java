package mappers;

import ch.helin.messages.dto.OrderDto;
import com.google.inject.Inject;
import commons.gis.GisHelper;
import models.Order;

import java.util.stream.Collectors;

public class OrderMapper {

    @Inject
    MissionMapper missionMapper;

    @Inject
    OrderProductsMapper orderProductsMapper;


    public OrderDto convertToOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();

        orderDto.setCustomerName(order.getCustomer().getDisplayName());
        orderDto.setDeliveryPosition(GisHelper.createPosition(order.getDeliveryPosition()));
        orderDto.setMissions(order.getMissions().stream().map(missionMapper::convertToMissionDto).collect(Collectors.toList()));
        orderDto.setOrderProducts(order.getOrderProducts().stream().map(orderProductsMapper::convertToOrderProductDto).collect(Collectors.toList()));
        orderDto.setProjectId(order.getProject().getId());
        orderDto.setState(order.getState().name());

        return orderDto;
    }


}