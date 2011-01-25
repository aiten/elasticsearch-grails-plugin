package org.grails.plugins.elasticsearch.util

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.ApplicationHolder

class DomainClassRegistry {

    public static GrailsDomainClass getDomainClass(instance) {
        GrailsApplication grailsApplication = ApplicationHolder.application
        GrailsDomainClass foundDomainClass = (GrailsDomainClass) grailsApplication.domainClasses.find { GrailsDomainClass domainClass ->
            domainClass.shortName == instance.class?.simpleName
        }
        return foundDomainClass
    }

}
