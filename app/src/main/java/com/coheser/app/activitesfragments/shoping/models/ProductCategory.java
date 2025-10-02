package com.coheser.app.activitesfragments.shoping.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductCategory implements Serializable {
    public String name;
    public ArrayList<ProductModel> productModels=new ArrayList<>();
}
