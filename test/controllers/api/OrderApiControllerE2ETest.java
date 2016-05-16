package controllers.api;

import ch.helin.messages.dto.OrderDto;
import com.google.inject.Inject;
import commons.AbstractE2ETest;
import commons.ImprovedTestHelper;
import mappers.RouteMapper;
import models.Mission;
import models.Order;
import models.Organisation;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class OrderApiControllerE2ETest extends AbstractE2ETest {


    private Organisation currentOrganisation;

    @Inject
    ImprovedTestHelper testHelper;

    @Inject
    ApiHelper apiHelper;

    @Inject
    RouteMapper routeMapper;

    @Before
    public void login() {
        currentOrganisation = doLogin();
    }


    @Test
    public void ShouldShowOrder() {
        Order order = jpaApi.withTransaction((em) -> {
            return testHelper.createNewOrderWithThreeMissions(
                    testHelper.createNewProject(currentOrganisation),
                    testHelper.createCustomer()
            );
        });

        OrderDto orderDto = apiHelper.doGet(routes.OrderApiController.show(order.getId()), OrderDto.class, browser);

        assertThat(orderDto.getState()).isEqualTo(order.getState().name());
        assertThat(orderDto.getCustomerName()).isEqualTo(order.getCustomer().getDisplayName());
        assertThat(orderDto.getMissions().size()).isEqualTo(order.getMissions().size());
        assertThat(orderDto.getOrderProducts().size()).isEqualTo(order.getOrderProducts().size());
        assertThat(orderDto.getDeliveryPosition()).isEqualTo(order.getDeliveryPosition().getPosition());
        assertThat(orderDto.getProjectId()).isEqualTo(order.getProject().getId());

        Mission firstMission = order.getMissions().iterator().next();
        assertThat(orderDto.getMissions().get(0).getRouteDto()).isEqualTo(routeMapper.convertToRouteDto(firstMission.getRoute()));


    }


}
