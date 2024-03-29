CREATE TABLE `task`
(
    `id`        CHAR(255) NOT NULL,
    `details`   TEXT      NOT NULL,
    `completed` BIT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE
);