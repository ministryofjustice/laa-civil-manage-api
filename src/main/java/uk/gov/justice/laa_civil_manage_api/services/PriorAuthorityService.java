package uk.gov.justice.laa_civil_manage_api.services;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;
import uk.gov.justice.laa_civil_manage_api.services.accessdatastore.AccessDataStoreClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriorAuthorityService {

    private final AccessDataStoreClient accessDataStoreClient;

    public PriorAuthorityApplicationResponse submit(PriorAuthority priorAuthority) {
        int documentCount = priorAuthority.uploadedDocuments() == null ? 0 : priorAuthority.uploadedDocuments().size();
        log.info("Submitting prior authority: applicationId={}, type={}, expertType={}, billingType={}, documentCount={}",
                priorAuthority.applicationId(),
                priorAuthority.type(),
                priorAuthority.expertType(),
                priorAuthority.billingType(),
                documentCount);

        PriorAuthorityApplicationResponse response = accessDataStoreClient.submitPriorAuthority(priorAuthority);

        log.info("Prior authority submitted: applicationId={}", priorAuthority.applicationId());
        return response;
    }
}
