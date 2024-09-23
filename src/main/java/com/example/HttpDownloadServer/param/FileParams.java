package com.example.HttpDownloadServer.param;

import com.example.HttpDownloadServer.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileParams {
    // File type (All/Video/Archive/Document/Photo)
    private String type;
    // Sort type (name/size/gmtCreated)
    private String sort;
    // Positive or reverse order (up/down)
    private String order;

    public void disposalFileParams() {
        if (type == null || type.isEmpty()) {
            this.type = Constants.DEFAULT_FILES_TYPE;
        }
        if (!"name".equals(sort) && !"size".equals(sort) && !"gmtCreated".equals(sort)) {
            this.sort = Constants.DEFAULT_FILES_SORT;
        }
        if (!"up".equals(order) && !"down".equals(order)) {
            this.order = Constants.DEFAULT_FILES_ORDER;
        }
    }
}
