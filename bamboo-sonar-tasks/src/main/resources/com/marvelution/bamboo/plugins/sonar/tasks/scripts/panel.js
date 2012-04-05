/*
 * This code is copied from the Bamboo distribution so.
 * Because of reusing the BAMBOO.LABELS object for managing the METRICS
 */

(function ($) {
    var BAMBOO = window.BAMBOO || {};
    BAMBOO.METRICS = (function () {
        var defaults = {
                trigger: ".metrics-edit",
                metricView: ".label-list, .label-none",
                metricsDialog: {
                    id: "metrics-dialog",
                    header: null,
                    width: 550,
                    height: 235,
                    shortcutKey: "M"
                },
                i18n: {
                    close: "Close"
                },
                templates: {
                    iconTemplate: '<span class="icon icon-{type}"></span>',
                    shortcutKeyTemplate: null
                }
            },
            options,
            $loadingIndicator,
            dialog,
            setupDialogContent = function (html) {
                var $html = $(html);
                $loadingIndicator.hide();
                dialog.addPanel("All", $html).show();
            },
            showDialog = function (e) {
                e.preventDefault();
                var $trigger = $(this);

                if (!$loadingIndicator) {
                    $loadingIndicator = $(AJS.template(options.templates.iconTemplate).fill({ type: "loading" }).toString()).insertAfter($trigger);
                } else {
                    $loadingIndicator.show();
                }

                dialog = new AJS.Dialog({
                    id: options.metricsDialog.id,
                    width: options.metricsDialog.width,
                    height: options.metricsDialog.height,
                    keypressListener: function (e) {
                        if (e.which == jQuery.ui.keyCode.ESCAPE) {
                            dialog.remove();
                        }
                    }
                });

                dialog.addCancel(options.i18n.close, function () { dialog.remove(); })
                      .addHeader(options.metricsDialog.header)
                      .addHelpText(options.templates.shortcutKeyTemplate, { shortcut: options.metricsDialog.shortcutKey });

                $.ajax({
                    url: $trigger.attr("href"),
                    data: { decorator: 'nothing', confirm: true },
                    success: setupDialogContent,
                    cache: false
                });
            },
            addMetrics = function (e) {
                e.preventDefault();
                var $form = $(this),
                    $input = $form.find("input:text").attr("readonly", "readonly"),
                    $submit = $form.find("input:submit").attr("disabled", "disabled"),
                    $loading = $(AJS.template(options.templates.iconTemplate).fill({ type: "loading" }).toString()).insertAfter($submit);
                $.post($form.attr("action"), $form.serialize(), function (data) {
                    $form.find(".error,.aui-message").remove();
                    if (typeof data == "object") {
                        // Returned data is JSON
                        if (data.fieldErrors) {
                            for (var fieldName in data.fieldErrors) {
                                BAMBOO.addFieldErrors($form, fieldName, data.fieldErrors[fieldName]);
                            }
                        }
                        if (data.errors) {
                            $form.prepend(BAMBOO.buildAUIErrorMessage(data.errors));
                        }
                    } else {
                        // Returned data isn't JSON, assume it's HTML
                    	console.log(data);
                        $(options.metricView).replaceWith(data);
                        $form.find("input:text").val("");
                    }
                    $input.removeAttr("readonly");
                    $submit.removeAttr("disabled");
                    $loading.remove();
                    $form.find("input:text").focus();
                });
            },
            deleteMetric = function (e) {
                e.preventDefault();
                $.post(this.href, function (data) {
                    $(options.metricView).replaceWith(data);
                });
            }
        return {
            init: function (opts) {
                options = $.extend(true, defaults, opts);
                $(document).delegate(options.trigger, "click", showDialog)
                           .delegate("#" + options.metricsDialog.id + " form", "submit", addMetrics)
                           .delegate("#" + options.metricsDialog.id + " .remove-label", "click", deleteMetric);
                $(function(){
                    AJS.whenIType(options.metricsDialog.shortcutKey).click(options.trigger);
                });
            }
        }
    })();
})(AJS.$);
