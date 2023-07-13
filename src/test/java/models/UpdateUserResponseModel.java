package models;

import lombok.Data;

@Data
public class UpdateUserResponseModel {
    String name;
    String job;
    String updatedAt;
}
