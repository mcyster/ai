

class PrehandlerActionContext extends PrehandlerContext {
    constructor(javaPrehandlerActionContext) {
        super(javaPrehandlerActionContext);
        this.javaPrehandlerActionContext = javaPrehandlerActionContext;
    }

    replaceCandidatePerson(person) {
        this.javaPrehandlerActionContext.replaceCandidatePerson(person);
    }

    addLogMessage(logMessage) {
        this.javaPrehandlerActionContext.addLogMessage(logMessage);
    }

    getEventBuilder() {
        return this.javaPrehandlerActionContext.getEventBuilder();
    }
}

class PrehandlerContext {
    constructor(javaPrehandlerContext) {
        this.javaPrehandlerContext = javaPrehandlerContext;
    }

    getRawEvent() {
        return this.javaPrehandlerContext.getRawEvent(); 
    }

    getProcessedRawEvent() {
        return this.javaPrehandlerContext.getProcessedRawEvent(); 
    }

    getCandidatePerson() {
        return this.javaPrehandlerContext.getCandidatePerson();
    }
}

class GlobalContext {
    constructor(javaGlobalContext) {
        this.javaGlobalContext = javaGlobalContext;
    }

    getClientContext() {
        return this.javaGlobalContext.getClientContext(); 
    }

    getGlobalServices() {
        return this.javaGlobalContext.getGlobalServices(); 
    }
}

class LoggerContext {
    constructor(javaLoggerContext) {
        this.javaLoggerContext = javaLoggerContext;
    }

    log(message) {
        this.javaLoggerContext.log(message); 
    }
}

class ClientContext {
    constructor(javaClientContext) {
        this.javaClientContext = javaClientContext;
    }

    getClientId() {
        return this.javaClientContext.getClientId(); 
    }

    getClientShortName() {
        return this.javaClientContext.getClientShortName(); 
    }

    getTimezone() {
        return this.javaClientContext.getTimezone(); 
    }
}

class GlobalServices {
    constructor(javaGlobalServices) {
        this.javaGlobalServices = javaGlobalServices;
    }

    getRandomService() {
        return this.javaGlobalServices.getRandomService(); 
    }

    getUnicodeService() {
        return this.javaGlobalServices.getUnicodeService(); 
    }

    getIntegerService() {
        return this.javaGlobalServices.getIntegerService(); 
    }

    getDoubleService() {
        return this.javaGlobalServices.getDoubleService(); 
    }

    getBigDecimalService() {
        return this.javaGlobalServices.getBigDecimalService(); 
    }

    getPublicClientDomainService() {
        return this.javaGlobalServices.getPublicClientDomainService(); 
    }

    getEncoderService() {
        return this.javaGlobalServices.getEncoderService(); 
    }

    getEmailVerificationService() {
        return this.javaGlobalServices.getEmailVerificationService(); 
    }

    getPersonService() {
        return this.javaGlobalServices.getPersonService(); 
    }

    getJsonService() {
        return this.javaGlobalServices.getJsonService(); 
    }

    getNotificationService() {
        return this.javaGlobalServices.getNotificationService(); 
    }

    getStringService() {
        return this.javaGlobalServices.getStringService(); 
    }

    getCouponService() {
        return this.javaGlobalServices.getCouponService(); 
    }

    getJwtService() {
        return this.javaGlobalServices.getJwtService(); 
    }

    getDateService() {
        return this.javaGlobalServices.getDateService(); 
    }

    getShareableService() {
        return this.javaGlobalServices.getShareableService(); 
    }

    getLanguage() {
        return this.javaGlobalServices.getLanguage(); 
    }
}

class ProcessedRawEventBuilder {
    constructor(javaProcessedRawEventBuilder) {
        this.javaProcessedRawEventBuilder = javaProcessedRawEventBuilder;
    }

    withClientDomain(clientDomain) {
        this.javaProcessedRawEventBuilder.withClientDomain(clientDomain);
        return this;
    }

    withEventName(eventName) {
        this.javaProcessedRawEventBuilder.withEventName(eventName);
        return this;
    }

    withEventTime(eventTime) {
        this.javaProcessedRawEventBuilder.withEventTime(eventTime);
        return this;
    }

    withSandbox(sandboxId) {
        this.javaProcessedRawEventBuilder.withSandbox(sandboxId);
        return this;
    }

    withAppType(appType) {
        this.javaProcessedRawEventBuilder.withAppType(appType);
        return this;
    }

    withDefaultAppType(defaultAppType) {
        this.javaProcessedRawEventBuilder.withDefaultAppType(defaultAppType);
        return this;
    }

    addSourceGeoIp(ipAddress) {
        this.javaProcessedRawEventBuilder.addSourceGeoIp(ipAddress);
        return this;
    }

    removeSourceGeoIp(ipAddress) {
        this.javaProcessedRawEventBuilder.removeSourceGeoIp(ipAddress);
        return this;
    }

    withDeviceId(deviceId) {
        this.javaProcessedRawEventBuilder.withDeviceId(deviceId);
        return this;
    }

    withPageId(pageId) {
        this.javaProcessedRawEventBuilder.withPageId(pageId);
        return this;
    }

    addJwt(jwt) {
        this.javaProcessedRawEventBuilder.addJwt(jwt);
        return this;
    }

    addData(data) {
        this.javaProcessedRawEventBuilder.addData(data);
        return this;
    }

    addData(name, value) {
        this.javaProcessedRawEventBuilder.addData(name, value);
        return this;
    }

    addVerifiedData(name, value) {
        this.javaProcessedRawEventBuilder.addVerifiedData(name, value);
        return this;
    }

    removeData(name) {
        this.javaProcessedRawEventBuilder.removeData(name);
        return this;
    }

    addAppData(data) {
        this.javaProcessedRawEventBuilder.addAppData(data);
        return this;
    }

    addAppData(name, value) {
        this.javaProcessedRawEventBuilder.addAppData(name, value);
        return this;
    }

    removeAppData(name) {
        this.javaProcessedRawEventBuilder.removeAppData(name);
        return this;
    }

    withDeviceType(deviceType) {
        this.javaProcessedRawEventBuilder.withDeviceType(deviceType);
        return this;
    }

    withDeviceOs(deviceOs) {
        this.javaProcessedRawEventBuilder.withDeviceOs(deviceOs);
        return this;
    }
}


