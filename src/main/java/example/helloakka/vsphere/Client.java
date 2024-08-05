package example.helloakka.vsphere;

import com.clovirvdi.domain.vdiinfra.entity.vo.VcenterConnectVo;
import com.clovirvdi.sddc.vsphere.client.VSphereClient;
import com.clovirvdi.sddc.vsphere.client.VSphereClientFactoryImpl;

public class Client {

    private static final VSphereClientFactoryImpl CLIENT_FACTORY = new VSphereClientFactoryImpl();

    public static VSphereClient getClient() {
        String username = "administrator@vsphere.local";
        String password = "VMware1!";
        String url = "cl-vcsa-01.cl.dom";

        return CLIENT_FACTORY.createClient(new VcenterConnectVo(url, username, password, "test", 1L));
    }
}
