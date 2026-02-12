package no.fint.consumer.consumer.utils;

import no.novari.fint.model.FintRelation;
import no.novari.fint.model.resource.FintResource;
import no.novari.metamodel.MetamodelService;
import no.novari.metamodel.model.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LinksCache {

    @Value("${fint.domainName}")
    private String domainName;

    @Value("${fint.packageName}")
    private String packageName;

    private final MetamodelService metamodelService;
    private final Map<String, List<FintRelation>> relationMap = new HashMap<>();

    public LinksCache(MetamodelService metamodelService) {
        this.metamodelService = metamodelService;
    }

    protected List<FintRelation> fetchRelations(String resourceName) {
        Resource resource = metamodelService.getResource(domainName, packageName, resourceName);
        return resource.getRelations();
    }

    private List<FintRelation> getCachedRelations(String resourceName) {
        return relationMap.computeIfAbsent(resourceName, this::fetchRelations);
    }

    public <T extends FintResource> void validateLinks(T resource, String resourceName) {
        List<FintRelation> fintRelations = getCachedRelations(resourceName);

        Set<String> allowedRelationNames = fintRelations.stream()
                .map(FintRelation::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        resource.getLinks().keySet().removeIf(linkName -> !allowedRelationNames.contains(linkName));
    }
}