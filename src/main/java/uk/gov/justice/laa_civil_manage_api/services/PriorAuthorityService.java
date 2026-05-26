package uk.gov.justice.laa_civil_manage_api.services;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Service
@RequiredArgsConstructor
public class PriorAuthorityService {

    private final AccessDataStoreClient accessDataStoreClient;

    public PriorAuthorityApplicationResponse submit(PriorAuthority priorAuthority) {
        return accessDataStoreClient.submitPriorAuthority(priorAuthority);
    }
}
