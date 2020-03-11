package com.gmail.andrewandy.ascendency.client.util;

import com.gmail.andrewandy.ascendency.common.util.Common;
import net.minecraftforge.common.ForgeHooks;

/**
 * Contains utility method which are client-specific.
 */
public class CommonClient {

    public static void tellClient(String... messages) {
        Common.tell(ForgeHooks.getCraftingPlayer(), messages);
    }

}
