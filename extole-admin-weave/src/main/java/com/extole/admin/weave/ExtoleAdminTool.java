package com.extole.admin.weave;

import com.cyster.ai.weave.service.Tool;
import com.extole.admin.weave.session.ExtoleSessionContext;

public interface ExtoleAdminTool<Request> extends Tool<Request, ExtoleSessionContext> {
}
