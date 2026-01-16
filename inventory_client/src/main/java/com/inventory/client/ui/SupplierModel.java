package com.inventory.client.ui;


public class SupplierModel {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;

    // --- Constructors ---
    public SupplierModel() {}

    public SupplierModel(String name, String contactPerson, String phone, String email, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Optional: toString for easier debugging
    @Override
    public String toString() {
        return name; // This helps if you put the object directly into a ComboBox
    }
}