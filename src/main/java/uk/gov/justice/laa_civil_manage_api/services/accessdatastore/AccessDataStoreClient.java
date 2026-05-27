package uk.gov.justice.laa_civil_manage_api.services.accessdatastore;

import uk.gov.justice.laa_civil_manage_api.models.PriorAuthority;
import uk.gov.justice.laa_civil_manage_api.models.PriorAuthorityApplicationResponse;

public interface AccessDataStoreClient {

    PriorAuthorityApplicationResponse submitPriorAuthority(PriorAuthority priorAuthority);
}
