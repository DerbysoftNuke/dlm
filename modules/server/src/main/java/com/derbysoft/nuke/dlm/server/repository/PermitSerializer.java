package com.derbysoft.nuke.dlm.server.repository;

import com.derbysoft.nuke.dlm.IPermit;
import com.derbysoft.nuke.dlm.PermitBuilderManager;
import com.derbysoft.nuke.dlm.PermitSpec;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;

/**
 * Created by passyt on 16-9-21.
 */
public class PermitSerializer implements Serializer<IPermit> {

    @Override
    public void serialize(@NotNull DataOutput2 out, @NotNull IPermit value) throws IOException {
        out.writeUTF(value.name());
        out.writeUTF(value.spec());
    }

    @Override
    public IPermit deserialize(@NotNull DataInput2 input, int available) throws IOException {
        String name = input.readUTF();
        String spec = input.readUTF();
        return PermitBuilderManager.getInstance().buildPermit(name, new PermitSpec(spec));
    }

}
