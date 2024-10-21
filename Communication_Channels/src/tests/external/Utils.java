package tests.external;


public class Utils {
	public final static String LOREM_IPSUM = "\n" + //
            "\n" + //
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id purus eu est viverra euismod ac sit amet est. Duis tincidunt aliquam condimentum. Donec a aliquam arcu, in gravida ligula. Quisque auctor nisi quam, vitae molestie risus consectetur in. Donec lacinia, velit cursus vulputate laoreet, ante magna consequat dolor, in ornare lectus lacus molestie est. Fusce in sodales nisl. Nullam a augue aliquet, accumsan nibh vitae, viverra nulla. Curabitur molestie libero id iaculis aliquet. Nulla facilisi. Integer ut pharetra est. Vestibulum molestie suscipit elementum. Sed interdum dui eros, in euismod erat suscipit eu.\n"
            + //
            "\n" + //
            "In sit amet accumsan justo. Quisque in nunc diam. Fusce non semper massa, non vestibulum mauris. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Vivamus rhoncus viverra tellus. Aliquam tincidunt augue tortor, id lacinia velit luctus in. Donec pretium ligula at posuere interdum. Nullam eros nisl, lacinia tincidunt mauris ac, sollicitudin aliquam ex. Morbi tempor tempor neque sed finibus. Vestibulum eget tortor sollicitudin, vestibulum ex elementum, tristique nunc. Ut congue lacinia lacus, vitae pretium mauris ultricies sit amet. Praesent faucibus venenatis tortor ac interdum. Quisque vel tincidunt neque. Ut eget efficitur massa.\n"
            + //
            "\n" + //
            "Nunc eget libero nec orci mollis convallis maximus et lectus. Donec ut malesuada mauris, at sollicitudin velit. Etiam et ex commodo, interdum enim et, faucibus arcu. Cras mollis mi massa, vitae efficitur mi ultrices vel. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi aliquam lectus vel sem pharetra facilisis. Nullam eget nisl at ex tempor suscipit. Vestibulum viverra orci id sapien luctus, nec faucibus lectus sagittis. Aenean maximus enim at dolor mollis, non tincidunt purus lacinia. Aliquam aliquam nisi felis. Curabitur elit arcu, fringilla non augue vitae, tempus ornare metus. Integer condimentum massa nec odio gravida scelerisque. Integer scelerisque vitae magna nec imperdiet.\n"
            + //
            "\n" + //
            "Suspendisse libero nisi, efficitur vel mauris vel, mollis fermentum diam. Nam accumsan lectus vitae tincidunt finibus. In tempus interdum arcu. Fusce dignissim venenatis ante id scelerisque. Integer finibus tristique lectus in feugiat. In sed sollicitudin metus, in tempor ligula. In commodo, nisi ut pellentesque convallis, arcu ligula ultricies metus, in faucibus odio quam non nisl. In nunc enim, scelerisque sollicitudin mattis id, ullamcorper nec dui. Mauris gravida mollis neque vitae facilisis. Donec nunc mi, condimentum nec metus in, congue hendrerit magna. Cras nunc eros, porttitor ut turpis in, hendrerit congue diam. Etiam id bibendum sem. Donec tempus erat at arcu maximus, id consequat nibh mattis. Cras diam orci, interdum at accumsan ut, fringilla vitae justo. Sed at efficitur eros, in volutpat odio.\n"
            + //
            "\n" + //
            "Pellentesque ornare est at quam rutrum, efficitur vehicula nibh convallis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam eleifend est viverra odio convallis eleifend. Vivamus nec ipsum vitae nisl efficitur fermentum sit amet nec nisl. Sed eget eros blandit, semper turpis sit amet, faucibus felis. Vestibulum molestie, lacus in blandit lacinia, nulla odio viverra mi, aliquam aliquam est turpis ullamcorper magna. Donec erat diam, vehicula a magna et, finibus commodo velit. Praesent tempus aliquet sem. Curabitur a scelerisque odio. ";

    public static byte[] getMessageSize(int size) {
        byte[] sizeBytes = new byte[4];
        sizeBytes[0] = (byte) (size >> 24);
        sizeBytes[1] = (byte) (size >> 16);
        sizeBytes[2] = (byte) (size >> 8);
        sizeBytes[3] = (byte) size;
        return sizeBytes;
    }

    public static int getSizeFromMessage(byte[] sizeBytes) {
        return (sizeBytes[0] << 24) | (sizeBytes[1] << 16) | (sizeBytes[2] << 8) | sizeBytes[3];
    }

    

}
