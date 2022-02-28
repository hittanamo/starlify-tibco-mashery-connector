package com.entiros.starlify.connector.api.service.impl;

import com.entiros.starlify.connector.api.dto.*;
import com.entiros.starlify.connector.api.dto.mashery.APIDefinitions;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPointDetails;
import com.entiros.starlify.connector.api.dto.mashery.APIEndPoints;
import com.entiros.starlify.connector.api.dto.mashery.MasheryResponse;
import com.entiros.starlify.connector.api.dto.starlify.*;
import com.entiros.starlify.connector.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarlifyProcessorImpl implements StarlifyProcessor {

    private final StarlifyService starlifyService;
    private final MashreyService mashreyService;

    private final Map<String, Map<String, NetworkSystem>> systemCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, NetworkSystem>> consumerSystemCache = new ConcurrentHashMap<>();
    private final Map<String, RequestItem> statusMap = new ConcurrentHashMap<>();

    private void processRequestIntrnl(Request request) {
        ((RequestItem) request).setStatus(RequestItem.Status.IN_PROCESS);
        MasheryResponse<APIDefinitions> apiDefinitions = mashreyService.getAPIDefinitions(request.getApiKey());

        List<NetworkSystem> systems = starlifyService.getSystems(request);
        this.populateSystems(request, systems);
        Map<String, NetworkSystem> existingSystems = systemCache.get(request.getNetworkId());
        List<APIDefinitions> defs = apiDefinitions.getCollection();
        defs.forEach(a -> {
            try {
                log.info("Started def:" + a.getName() + " id:" + a.getId());
                NetworkSystem networkSystem = existingSystems != null ? existingSystems.get(a.getName()) : null;
                String systemId = null;
                if (networkSystem == null) {
                    SystemDto systemDto = this.createSystemDto(request, a.getName(), a.getDescription());
                    SystemRespDto systemRespDto = starlifyService.addSystem(request, systemDto);
                    systemId = systemRespDto.getId();
                } else {
                    systemId = networkSystem.getId();
                }
                Response<ServiceRespDto> services = starlifyService.getServices(request, systemId);
                Set<String> serviceNames = this.getServiceNames(services);
                MasheryResponse<APIEndPoints> apiEndpoints = mashreyService.getApiEndpoints(request.getApiKey(), a.getId());

                if (apiEndpoints.getCollection() != null && !apiEndpoints.getCollection().isEmpty()) {
                    log.info("Endpoints size :" + apiEndpoints.getCollection().size());
                    for (APIEndPoints ep : apiEndpoints.getCollection()) {
                        try {
                            APIEndPointDetails epd = mashreyService.getEndpointDetails(request.getApiKey(), a.getId(), ep.getId());
                            if (epd == null) {
                                log.info("empty details:");
                                continue;
                            }

                            String name = getHttpMethod(epd) + " " + ep.getName();
                            if (!serviceNames.contains(name)) {
                                ServiceDto dto = new ServiceDto();
                                dto.setName(name);
                                starlifyService.addService(request, dto, systemId);
                            }
                        } catch (Throwable e) {
                            log.error("Error while processing service:" + a.getName(), e);
                        }
                    }
                    ((RequestItem) request).setStatus(RequestItem.Status.DONE);
                    log.info("Started asset:" + a.getName());
                }
            } catch (Throwable t) {
                log.error("Error while processing asset:" + a.getName(), t);
                ((RequestItem) request).setStatus(RequestItem.Status.ERROR);
            }
        });
        // clearing cache
        consumerSystemCache.remove(request.getNetworkId());
        systemCache.remove(request.getNetworkId());
    }

    private String getHttpMethod(APIEndPointDetails epd) {
        if (epd.isDelete()) {
            return "Delete";
        } else if (epd.isGet()) {
            return "Get";
        } else if (epd.isHead()) {
            return "Head";
        } else if (epd.isPatch()) {
            return "Patch";
        } else if (epd.isPost()) {
            return "Post";
        } else if (epd.isPut()) {
            return "Put";
        } else {
            return "Options";
        }
    }

    private SystemDto createSystemDto(Request request, String name, String description) {
        SystemDto s = new SystemDto();
        String id = UUID.randomUUID().toString();
        s.setId(id);
        s.setName(name);
        Network n = new Network();
        n.setId(request.getNetworkId());
        s.setNetwork(n);
        s.setDescription(description);
        return s;
    }

    private synchronized Set<String> getServiceNames(Response<ServiceRespDto> services) {
        List<ServiceRespDto> content = services.getContent();
        Set<String> ret = new HashSet<>();
        if (content != null && !content.isEmpty()) {
            for (ServiceRespDto c : content) {
                ret.add(c.getName());
            }
        }
        return ret;
    }

    @Override
    public RequestItem processRequest(Request request) {
        RequestItem workItem = new RequestItem();
        workItem.setStatus(RequestItem.Status.NOT_STARTED);
        workItem.setStarlifyKey(request.getStarlifyKey());
        workItem.setApiKey(request.getApiKey());
        workItem.setNetworkId(request.getNetworkId());
        statusMap.put(request.getNetworkId(), workItem);
        CompletableFuture.runAsync(() -> {
            try {
                processRequestIntrnl(workItem);
            } catch (Throwable t) {
                log.error("Error while processing:", t);
                workItem.setStatus(RequestItem.Status.ERROR);
            }

        });
        return workItem;
    }


    @Override
    public RequestItem status(Request request) {
        return statusMap.get(request.getNetworkId());
    }

    private SystemDto createSystemDto(Request request, String name) {
        SystemDto s = new SystemDto();
        String id = UUID.randomUUID().toString();
        s.setId(id);
        s.setName(name);
        Network n = new Network();
        n.setId(request.getNetworkId());
        s.setNetwork(n);
        return s;
    }

    private synchronized void populateSystems(Request request, List<NetworkSystem> networkSystems) {
        if (networkSystems != null && !networkSystems.isEmpty()) {
            Map<String, NetworkSystem> existingSystems = systemCache.get(request.getNetworkId());
            if (existingSystems == null) {
                existingSystems = new ConcurrentHashMap<>();
                systemCache.put(request.getNetworkId(), existingSystems);
            }
            for (NetworkSystem ns : networkSystems) {
                existingSystems.put(ns.getName(), ns);
            }
        }
    }

    private synchronized void updateCache(String networkId, NetworkSystem networkSystem) {
        if (networkSystem != null) {
            Map<String, NetworkSystem> existingSystems = systemCache.get(networkId);
            if (existingSystems == null) {
                existingSystems = new ConcurrentHashMap<>();
                systemCache.put(networkId, existingSystems);
            }
            existingSystems.put(networkSystem.getName(), networkSystem);
        }
    }
}
