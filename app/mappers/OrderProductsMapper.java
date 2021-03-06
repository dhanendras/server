package mappers;

import ch.helin.messages.dto.OrderProductDto;
import com.google.inject.Inject;
import models.OrderProduct;

public class OrderProductsMapper {

    @Inject
    private ProductMapper productMapper;

    public OrderProductDto convertToOrderProductDto(OrderProduct orderProduct) {
        OrderProductDto orderProductDto = new OrderProductDto();

        orderProductDto.setAmount(orderProduct.getAmount());
        orderProductDto.setProduct(productMapper.convertToProductDto(orderProduct.getProduct()));
        orderProductDto.setTotalPrice(orderProduct.getTotalPrice());

        return orderProductDto;
    }




}
