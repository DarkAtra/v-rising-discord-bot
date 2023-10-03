package de.darkatra.vrising.discord.commands.parameters

import de.darkatra.vrising.discord.commands.exceptions.ValidationException
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator
import java.net.InetAddress
import java.net.UnknownHostException

object HostnameValidator {

    fun validate(hostname: String, allowLocalAddressRanges: Boolean, propertyName: String) {
        if (!InetAddressValidator.getInstance().isValid(hostname) && !DomainValidator.getInstance(true).isValid(hostname)) {
            throw ValidationException("'$propertyName' is not a valid ip address or domain name. Rejected: $hostname")
        }
        if (!allowLocalAddressRanges) {
            val resolvedAddress = try {
                InetAddress.getByName(hostname)
            } catch (e: UnknownHostException) {
                throw ValidationException("'$propertyName' could not be resolved. Rejected: $hostname")
            }
            if (resolvedAddress.isSiteLocalAddress || resolvedAddress.isLoopbackAddress || resolvedAddress.isLinkLocalAddress) {
                throw ValidationException("'$propertyName' must be a public ip address. Rejected: $hostname")
            }
        }
    }
}
