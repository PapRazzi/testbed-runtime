package de.uniluebeck.itm.tr.snaa.shiro.rest;

import de.uniluebeck.itm.tr.snaa.shiro.dto.ActionDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/actions")
public interface ActionResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ActionDto> listActions();

}
