package com.nesp.linux.changewall.model

data class Settings(
        val cycle_time: Long = 30 * 1000,
        val delete_files_before_dl_biying_img: Boolean = true,
        val dl_biying_img_dir: String = "biying",
        val dl_biying_img_max_count: Int = 20,
        val dl_biying_img_mode: Int = 0,
        val dl_delay_time: String = "1d",
//        val linux_root_pass: String = "",
        val wall_img_dir: String = "biying"
//        val change_lock_background: Boolean = false,
//        val blur_lock_background: Boolean = false
)