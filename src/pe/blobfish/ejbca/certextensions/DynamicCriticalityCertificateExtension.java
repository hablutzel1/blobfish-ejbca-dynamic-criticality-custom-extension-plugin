package pe.blobfish.ejbca.certextensions;

import org.apache.log4j.Logger;
import org.cesecore.certificates.ca.CA;
import org.cesecore.certificates.ca.internal.CertificateValidity;
import org.cesecore.certificates.certificate.certextensions.BasicCertificateExtension;
import org.cesecore.certificates.certificate.certextensions.CertificateExtensionException;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.endentity.EndEntityInformation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PublicKey;

// TODO determine if there is any way to provide a custom message in the "System Configuration > Custom Certificate Extensions" GUI for this type of extensions to indicate that the criticality configured there is the fallback criticality in case the user doesn't send anything.
public class DynamicCriticalityCertificateExtension extends BasicCertificateExtension {

    private static final Logger log = Logger.getLogger(DynamicCriticalityCertificateExtension.class);

    // TODO check: for serialization purposes, shouldn't we include an static variable like org.cesecore.certificates.certificate.certextensions.standard.SubjectKeyIdentifier.serialVersionUID?. Not sure but it seems that if there exists a saved instance of this extension in EJBCA and any serializable field changes in this class, EJBCA fails during the next load. Refresh appropriately on serialization to work on this.

    // FIXME duplicated initialization code in pe.blobfish.ejbca.certextensions.DynamicCriticalityCertificateExtension.readObject.
    private transient ThreadLocal<EndEntityInformation> userData = new ThreadLocal<>();

    public static final String DISPLAY_NAME = "Dynamic Criticality Certificate Extension";


    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        userData = new ThreadLocal<>();
    }

    // TODO Check if this is initializer called as part of the deserialization. I think it isn't but refresh with the JLS.
    {
        // TODO confirm why is it being called from here instead of the constructor.
        setDisplayName(DISPLAY_NAME);
    }

    // TODO Check if this is called as part of the deserialization. I think it isn't but refresh with the JLS.
    public DynamicCriticalityCertificateExtension() {
    }

    @Override
    public byte[] getValueEncoded(EndEntityInformation userData, CA ca, CertificateProfile certProfile, PublicKey userPublicKey, PublicKey caPublicKey, CertificateValidity val) throws CertificateExtensionException {
        this.userData.set(userData);
        return super.getValueEncoded(userData, ca, certProfile, userPublicKey, caPublicKey, val);
    }

    /**
     * NOTE that during certificate generation it depends on {@link DynamicCriticalityCertificateExtension#getValueEncoded} being called first.
     */
    @Override
    public boolean isCriticalFlag() {
        if (userData.get() == null) { // We are at "System Configuration > Custom Certificate Extensions".
            log.debug("DynamicCriticalityCertificateExtension.isCriticalFlag was called from a place different than the certificate generation process, e.g. \"System Configuration > Custom Certificate Extensions\" GUI");
            return super.isCriticalFlag();
        }

        boolean criticalityValue;
        // TODO clarify the difference between org.cesecore.certificates.endentity.ExtendedInformation.CUSTOMDATA and org.cesecore.certificates.endentity.ExtendedInformation.EXTENSIONDATA and check if maybe the criticality should come as CUSTOMDATA.
        String extensionCriticality = userData.get().getExtendedInformation().getExtensionData(getOID() + ".critical");
        if (extensionCriticality != null) {
            // TODO check: really needed to use java.lang.String.equalsIgnoreCase?, consider that this could have been configured from a properties file, check if the configuration with properties file is clearly documenting the usage of lowercase "true" or "false". Consider to do a call to java.lang.Boolean.valueOf(java.lang.String) as it would be clearer.
            criticalityValue = extensionCriticality.trim().equalsIgnoreCase("true");
        } else {
            // TODO check: the value configured from the GUI/(configuration file) could be used as a default if the user doesn't send the '<OID>.critical'. value. If this value would be taken as default this type of configuration shouldn't be allowed: "id1.critical=ignored". Note that possibly the "Required" property could be used to enforce the existence of the '<OID>.critical' attribute.
            log.warn("Dynamic Criticality Certificate Extension ("+getOID()+"): Defaulting to configured critical flag because no criticality value was sent as extended information.");
            criticalityValue = super.isCriticalFlag();
        }
        userData.remove(); // Just to get sure that the next call to this method requires a previous call to 'getValueEncoded'.
        return criticalityValue;
    }

}
