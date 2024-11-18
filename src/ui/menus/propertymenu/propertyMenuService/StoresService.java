package ui.menus.propertymenu.propertyMenuService;

import model.clients.Owner;
import model.exceptions.DuplicateElementException;
import model.exceptions.InvalidInputException;
import model.genericManagement.GenericClass;
import model.genericManagement.JsonUtils;
import model.properties.Orientation;
import model.properties.Property;
import model.properties.Store;
import model.utils.Utils;

import java.util.Scanner;

public class StoresService {

    Scanner scanner;
    GenericClass<Property> properties;
    GenericClass<Owner> owners;

    public StoresService() {
        scanner = new Scanner(System.in);
        properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
        owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
    }

    public void addStore() {
        Boolean continueAdding = true;
        do {
            try {
                properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
                owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));
                Store newStore = createStore(scanner, owners);

                System.out.println("Store added successfully:");
                System.out.println(newStore);
                if (!properties.isEmpty()) {
                    Property p = properties.getLastObject();
                    Integer lastId = p.getId() + 1;
                    newStore.setId(lastId);
                }
                properties.addElement(newStore);
                JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);
            } catch (InvalidInputException e) {
                System.out.println("Error adding Store: " + e.getMessage());
            } catch (DuplicateElementException e) {
                System.out.println("Error: " + e.getMessage());
            }
            continueAdding = askToContinue();
        } while (continueAdding);
    }

    public static Store createStore(Scanner scanner, GenericClass<Owner> ownerList) throws InvalidInputException {
        Owner owner = new Owner();
        System.out.print("Enter the owner's DNI: ");
        String ownerDni = scanner.nextLine().trim();
        owner = validateOwner(ownerDni, ownerList);

        System.out.print("Enter Store address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Enter area: ");
        Double area = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter sales Price: ");
        Double sp = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter Rental Price: ");
        Double rp = Double.parseDouble(scanner.nextLine().trim());

        System.out.println("Enter bathrooms quantity: ");
        Integer bathroomsQuantity = Integer.parseInt(scanner.nextLine().trim());

        int flag = 0;
        Orientation orientation = null;
        do {
            System.out.println("what is the orientation? (1.FRONT / 2. BACK)");
            flag = Integer.parseInt(scanner.nextLine().trim());
            if (flag == 1) {
                orientation = Orientation.FRONT;
            } else if (flag == 2) {
                orientation = Orientation.BACK;
            }
        }
        while (flag != 1 && flag != 2);

        System.out.println("Enter floors quantity: ");
        Integer floors = Integer.parseInt(scanner.nextLine().trim());


        validateStoreInputs(address, area, sp, rp, bathroomsQuantity, floors);

        return new Store(owner, address, area, sp, rp, bathroomsQuantity, orientation, floors);
    }

    public static Owner validateOwner(String ownerDni, GenericClass<Owner> ownerList) throws InvalidInputException {
        Owner owner = null;
        for (Owner ow : ownerList.returnList()) {
            if (ow.getDni().equalsIgnoreCase(ownerDni)) {
                owner = ow;
                break;
            }
        }
        if (owner == null) {
            throw new InvalidInputException("Owner with DNI " + ownerDni + " not found.");
        } else {
            return owner;
        }
    }

    public static void validateStoreInputs(String address, Double area, Double sp, Double rp, Integer bathroomsQuantity, Integer floors) throws InvalidInputException {
        if (address.isEmpty()) {
            throw new InvalidInputException("address cannot be empty.");
        }
        if (area.isNaN() || area <= 0) {
            throw new InvalidInputException("area is not a number.");
        }
        if (sp.isNaN() || sp <= 0) {
            throw new InvalidInputException("Sales price is not a number.");
        }
        if (rp.isNaN() || rp <= 0) {
            throw new InvalidInputException("Rental price is not a number.");
        }
        if (bathroomsQuantity < 0) {
            throw new InvalidInputException("Bathrooms Quantity must be a positive number.");
        }
        if (floors < 0) {
            throw new InvalidInputException("Floors quantity must be a positive number or null.");
        }
    }

    private Boolean askToContinue() {
        System.out.print("Do you want to add another Store? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("yes") || response.equals("y");
    }

    public void modifyStore() {
        Boolean continueModifying = true;
        do {
            try {
                properties = new GenericClass<>(JsonUtils.loadList("properties.json", Property.class));
                owners = new GenericClass<>(JsonUtils.loadList("owners.json", Owner.class));

                System.out.print("Enter the ID of the store to modify: ");
                Integer storeId = Integer.parseInt(scanner.nextLine().trim());

                Store storeToModify = findStoreById(storeId);

                if (storeToModify == null) {
                    throw new InvalidInputException("Store with ID " + storeId + " not found.");
                }

                System.out.println(storeToModify);

                modifyStoreDetails(storeToModify);

                JsonUtils.saveList(properties.returnList(), "properties.json", Property.class);

                System.out.println("Store modified successfully: " + storeToModify);

            } catch (InvalidInputException | NumberFormatException e) {
                System.out.println("Error modifying store: " + e.getMessage());
            }

            continueModifying = askToContinue();
        } while (continueModifying);
    }

    private Store findStoreById(Integer storeId) {
        for (Property property : properties.returnList()) {
            if (property instanceof Store) {
                Store store = (Store) property;
                if (store.getId().equals(storeId)) {
                    return store;
                }
            }
        }
        return null;
    }

    private void modifyStoreDetails(Store store) throws InvalidInputException {
        Boolean continueModifying = true;
        Integer option;

        do {
            System.out.println("\n----------------------------------------------------");
            System.out.println("               Modify Store Details");
            System.out.println("----------------------------------------------------");
            System.out.println("1. Address");
            System.out.println("2. Area");
            System.out.println("3. Sales Price");
            System.out.println("4. Rental Price");
            System.out.println("5. Bathrooms Quantity");
            System.out.println("6. Orientation (FRONT/BACK)");
            System.out.println("7. Floors Quantity");
            System.out.println("0. Go back");
            System.out.println("----------------------------------------------------");
            System.out.println("Please select the detail you would like to modify:");

            option = Utils.getValidatedOption();

            switch (option) {
                case 1:
                    System.out.print("Address (" + store.getAdress() + "): ");
                    String newAddress = scanner.nextLine().trim();
                    if (!newAddress.isEmpty()) {
                        store.setAdress(newAddress);
                    }
                    break;

                case 2:
                    System.out.print("Area (" + store.getArea() + "): ");
                    String newArea = scanner.nextLine().trim();
                    if (!newArea.isEmpty()) {
                        Double area = Double.parseDouble(newArea);
                        validateArea(area);
                        store.setArea(area);
                    }
                    break;

                case 3:
                    System.out.print("Sales Price (" + store.getSalesPrice() + "): ");
                    String newSalesPrice = scanner.nextLine().trim();
                    if (!newSalesPrice.isEmpty()) {
                        double sp = Double.parseDouble(newSalesPrice);
                        validatePrice(sp);
                        store.setSalesPrice(sp);
                    }
                    break;

                case 4:
                    System.out.print("Rental Price (" + store.getRentalPrice() + "): ");
                    String newRentalPrice = scanner.nextLine().trim();
                    if (!newRentalPrice.isEmpty()) {
                        double rp = Double.parseDouble(newRentalPrice);
                        validatePrice(rp);
                        store.setRentalPrice(rp);
                    }
                    break;

                case 5:
                    System.out.print("Bathrooms Quantity (" + store.getBathRooms() + "): ");
                    String newBathrooms = scanner.nextLine().trim();
                    if (!newBathrooms.isEmpty()) {
                        int bathrooms = Integer.parseInt(newBathrooms);
                        validateBathrooms(bathrooms);
                        store.setBathRooms(bathrooms);
                    }
                    break;

                case 6:
                    System.out.print("Orientation (FRONT/BACK) (" + store.getOrientation() + "): ");
                    String newOrientation = scanner.nextLine().trim().toUpperCase();
                    if (newOrientation.equals("FRONT") || newOrientation.equals("BACK")) {
                        store.setOrientation(Orientation.valueOf(newOrientation));
                    } else {
                        System.out.println("Invalid orientation. Value remains unchanged.");
                    }
                    break;

                case 7:
                    System.out.print("Floors Quantity (" + store.getFloorsQuantity() + "): ");
                    String newFloors = scanner.nextLine().trim();
                    if (!newFloors.isEmpty()) {
                        int floors = Integer.parseInt(newFloors);
                        validateFloors(floors);
                        store.setFloorsQuantity(floors);
                    }
                    break;

                case 0:
                    System.out.println("Returning to the previous menu...");
                    break;

                default:
                    System.out.println("Invalid option. Please choose a valid number.");
                    break;
            }

            if (option != 0) {
                System.out.print("Do you want to modify another detail? (Y/N): ");
                String continueResponse = scanner.nextLine().trim();
                continueModifying = continueResponse.equalsIgnoreCase("Y");
            } else {
                continueModifying = false;
            }

        } while (continueModifying);
    }

    private void validateArea(double area) throws InvalidInputException {
        if (area <= 0) {
            throw new InvalidInputException("Area must be a positive number.");
        }
    }

    private void validatePrice(double price) throws InvalidInputException {
        if (price <= 0) {
            throw new InvalidInputException("Price must be a positive number.");
        }
    }

    private void validateBathrooms(int bathrooms) throws InvalidInputException {
        if (bathrooms < 0) {
            throw new InvalidInputException("Bathrooms quantity must be a positive number.");
        }
    }

    private void validateFloors(int floors) throws InvalidInputException {
        if (floors < 0) {
            throw new InvalidInputException("Floors quantity must be a positive number.");
        }
    }
}