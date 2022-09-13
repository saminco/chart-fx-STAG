/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) 2009 by Vinnie Falco
 * Copyright (c) 2016 by Bernd Porr
 * Copyright (c) 2019 by Ralph J. Steinhagen
 */

package de.gsi.math.filter.iir;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * User facing class which contains all the methods the user uses to create Butterworth filters. This done in this way:
 * Butterworth butterworth = new Butterworth(); Then call one of the methods below to create low-,high-,band-, or
 * stopband filters. For example: butterworth.bandPass(2,250,50,5);
 */
public class Butterworth extends Cascade {
    /**
     * Band-pass filter with default topology
     *
     * @param order filter order (actual order is twice)
     * @param sampleRate sampling rate of the system
     * @param centerFrequency centre frequency
     * @param widthFrequency width of the notch
     */
    public void bandPass(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency) {
        setupBandPass(order, sampleRate, centerFrequency, widthFrequency, DirectFormAbstract.DIRECT_FORM_II);
    }

    /**
     * Band-pass filter with custom topology
     *
     * @param order filter order (actual order is twice)
     * @param sampleRate sampling rate of the system
     * @param centerFrequency centre frequency
     * @param widthFrequency width of the notch
     * @param directFormType filter topology
     */
    public void bandPass(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency, final int directFormType) {
        setupBandPass(order, sampleRate, centerFrequency, widthFrequency, directFormType);
    }

    /**
     * Band-stop filter with default topology
     *
     * @param order filter order (actual order is twice)
     * @param sampleRate sampling rate of the system
     * @param centerFrequency centre frequency
     * @param widthFrequency width of the notch
     */
    public void bandStop(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency) {
        setupBandStop(order, sampleRate, centerFrequency, widthFrequency, DirectFormAbstract.DIRECT_FORM_II);
    }

    /**
     * Band-stop filter with custom topology
     *
     * @param order filter order (actual order is twice)
     * @param sampleRate sampling rate of the system
     * @param centerFrequency centre frequency
     * @param widthFrequency width of the notch
     * @param directFormType filter topology
     */
    public void bandStop(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency, final int directFormType) {
        setupBandStop(order, sampleRate, centerFrequency, widthFrequency, directFormType);
    }

    /**
     * High-pass filter with default filter topology
     *
     * @param order filter order (ideally only even orders)
     * @param sampleRate sampling rate of the system
     * @param cutoffFrequency cutoff of the system
     */
    public void highPass(final int order, final double sampleRate, final double cutoffFrequency) {
        setupHighPass(order, sampleRate, cutoffFrequency, DirectFormAbstract.DIRECT_FORM_II);
    }

    /**
     * High-pass filter with custom topology
     *
     * @param order filter order (ideally only even orders)
     * @param sampleRate sSampling rate of the system
     * @param cutoffFrequency cutoff of the system
     * @param directFormType filter topology. See DirectFormAbstract.
     */
    public void highPass(final int order, final double sampleRate, final double cutoffFrequency,
            final int directFormType) {
        setupHighPass(order, sampleRate, cutoffFrequency, directFormType);
    }

    /**
     * Butterworth Low-pass filter with default topology
     *
     * @param order the order of the filter
     * @param sampleRate sampling rate of the system
     * @param cutoffFrequency cutoff frequency
     */
    public void lowPass(final int order, final double sampleRate, final double cutoffFrequency) {
        setupLowPass(order, sampleRate, cutoffFrequency, DirectFormAbstract.DIRECT_FORM_II);
    }

    /**
     * Butterworth Low-pass filter with custom topology
     *
     * @param order the order of the filter
     * @param sampleRate sampling rate of the system
     * @param cutoffFrequency cutoff frequency
     * @param directFormType filter topology. This is either DirectFormAbstract.DIRECT_FORM_I or DIRECT_FORM_II
     */
    public void lowPass(final int order, final double sampleRate, final double cutoffFrequency,
            final int directFormType) {
        setupLowPass(order, sampleRate, cutoffFrequency, directFormType);
    }

    private void setupBandPass(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency, final int directFormType) {
        final AnalogLowPass analogProto = new AnalogLowPass(order);
        analogProto.design();

        final LayoutBase digitalProto = new LayoutBase(order * 2);

        BandPassTransform.transform(centerFrequency / sampleRate, widthFrequency / sampleRate, digitalProto,
                analogProto);

        setLayout(digitalProto, directFormType);
    }

    private void setupBandStop(final int order, final double sampleRate, final double centerFrequency,
            final double widthFrequency, final int directFormType) {
        final AnalogLowPass analogProto = new AnalogLowPass(order);
        analogProto.design();

        final LayoutBase digitalProto = new LayoutBase(order * 2);

        BandStopTransform.transform(centerFrequency / sampleRate, widthFrequency / sampleRate, digitalProto,
                analogProto);

        setLayout(digitalProto, directFormType);
    }

    private void setupHighPass(final int order, final double sampleRate, final double cutoffFrequency,
            final int directFormType) {
        final AnalogLowPass analogProto = new AnalogLowPass(order);
        analogProto.design();

        final LayoutBase digitalProto = new LayoutBase(order);

        HighPassTransform.transform(cutoffFrequency / sampleRate, digitalProto, analogProto);

        setLayout(digitalProto, directFormType);
    }

    private void setupLowPass(final int order, final double sampleRate, final double cutoffFrequency,
            final int directFormType) {
        final AnalogLowPass analogProto = new AnalogLowPass(order);
        analogProto.design();

        final LayoutBase digitalProto = new LayoutBase(order);

        LowPassTransform.transform(cutoffFrequency / sampleRate, digitalProto, analogProto);

        setLayout(digitalProto, directFormType);
    }

    private static class AnalogLowPass extends LayoutBase {
        private final int nPoles;

        public AnalogLowPass(final int nPoles) {
            super(nPoles);
            this.nPoles = nPoles;
            setNormal(0, 1);
        }

        public void design() {
            reset();
            final double n2 = 2.0 * nPoles;
            final int pairs = nPoles / 2;
            for (int i = 0; i < pairs; ++i) {
                final Complex c = ComplexUtils.polar2Complex(1F, Math.PI / 2.0 + (2 * i + 1) * Math.PI / n2);
                addPoleZeroConjugatePairs(c, Complex.INF);
            }

            if ((nPoles & 1) == 1) {
                add(new Complex(-1), Complex.INF);
            }
        }
    }
}
