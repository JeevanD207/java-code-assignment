package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
     LocationGateway locationGateway = new LocationGateway();

    // when
     Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
     assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void returnNullForUnknowLocation(){
    LocationGateway locationGateway = new LocationGateway();
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001"));
  }
}
